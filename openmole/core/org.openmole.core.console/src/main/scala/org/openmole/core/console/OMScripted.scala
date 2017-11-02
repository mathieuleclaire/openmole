package org.openmole.core.console

import scala.language.dynamics
import scala.beans.BeanProperty
import scala.collection.JavaConverters._
import scala.reflect.{ ClassTag, classTag }
import scala.reflect.internal.util.Position
import scala.tools.nsc.util.stringFromReader
import javax.script._
import ScriptContext.{ ENGINE_SCOPE, GLOBAL_SCOPE }
import java.io.{ Closeable, Reader }

import org.openmole.core.console.ScalaREPL.OMIMain

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain.{ defaultOut, defaultSettings }
import scala.tools.nsc.interpreter.{ IMain, IR, JPrintWriter, OutputStream, ReplReporter, Scripted, WriterOutputStream, isReplDebug }
import scala.tools.nsc.reporters.Reporter

object OMScripted {

  def call[U: ClassTag, T](o: AnyRef, name: String, args: Vector[Any]) = {
    val handle = implicitly[ClassTag[U]].runtimeClass.getDeclaredMethods.find(_.getName == name).get
    handle.setAccessible(true)
    handle.invoke(o, args.toArray.map(_.asInstanceOf[Object]): _*).asInstanceOf[T]
  }
}

/* A REPL adaptor for the javax.script API. */
class OMScripted(val factory: ScriptEngineFactory, settings: Settings, out: JPrintWriter, val omIMain: OMIMain)
  extends AbstractScriptEngine with Compilable {

  def createBindings: Bindings = new SimpleBindings

  // dynamic context bound under this name
  final val ctx = "$ctx"
  val intp = omIMain

  var compileContext: ScriptContext = getContext

  val scriptContextRep = new intp.ReadEvalPrint

  def dynamicContext_=(ctx: ScriptContext): Unit = scriptContextRep.callEither("set", ctx)

  def dynamicContext: ScriptContext = scriptContextRep.callEither("value") match {
    case Right(ctx: ScriptContext) ⇒ ctx
    case Left(e)                   ⇒ throw e
    case Right(other)              ⇒ throw new ScriptException(s"Unexpected value for context: $other")
  }

  if (intp.isInitializeComplete) {
    // compile the dynamic ScriptContext object holder
    val ctxRes = scriptContextRep compile s"""
                                             |import _root_.javax.script._
                                             |object ${scriptContextRep.evalName} {
                                             |  var value: ScriptContext = _
                                             |  def set(x: _root_.scala.Any) = value = x.asInstanceOf[ScriptContext]
                                             |}
    """.stripMargin
    if (!ctxRes) throw new ScriptException("Failed to compile ctx")
    dynamicContext = getContext

    // Bridge dynamic references and script context
    val dynRes = intp compileString s"""
                                       |package scala.tools.nsc.interpreter
                                       |import _root_.scala.language.dynamics
                                       |import _root_.javax.script._, ScriptContext.ENGINE_SCOPE
                                       |object dynamicBindings extends _root_.scala.Dynamic {
                                       |  def context: ScriptContext = ${scriptContextRep.evalPath}.value
                                       |  // $ctx.x retrieves the attribute x
                                       |  def selectDynamic(field: _root_.java.lang.String): _root_.java.lang.Object = context.getAttribute(field)
                                       |  // $ctx.x = v
                                       |  def updateDynamic(field: _root_.java.lang.String)(value: _root_.java.lang.Object) = context.setAttribute(field, value, ENGINE_SCOPE)
                                       |}
                                       |""".stripMargin
    if (!dynRes) throw new ScriptException("Failed to compile dynamicBindings")
    intp beQuietDuring {
      intp interpret s"val $ctx: _root_.scala.tools.nsc.interpreter.dynamicBindings.type = _root_.scala.tools.nsc.interpreter.dynamicBindings"
      intp bind ("$engine" → (this: ScriptEngine with Compilable))
    }
  }

  // Set the context for dynamic resolution and run the body.
  // Defines attributes available for evaluation.
  // Avoid reflective access if using default context.
  def withScriptContext[A](context: ScriptContext)(body: ⇒ A): A =
    if (context eq getContext) body else {
      val saved = dynamicContext
      dynamicContext = context
      try body
      finally dynamicContext = saved
    }
  // Defines attributes available for compilation.
  def withCompileContext[A](context: ScriptContext)(body: ⇒ A): A = {
    val saved = compileContext
    compileContext = context
    try body
    finally compileContext = saved
  }

  // not obvious that ScriptEngine should accumulate code text
  private var code = ""

  private var firstError: Option[(Position, String)] = None

  /* All scripts are compiled. The supplied context defines what references
   * not in REPL history are allowed, though a different context may be
   * supplied for evaluation of a compiled script.
   */
  def compile(script: String, context: ScriptContext): CompiledScript =
    withCompileContext(context) {
      val cat = code + script
      //intp.compile(cat, synthetic = false)
      OMScripted.call[IMain, Either[IR.Result, intp.Request]](intp, "compile", Vector(cat, false)) match {
        case Right(req) ⇒
          code = ""
          new WrappedRequest(req)
        case Left(IR.Incomplete) ⇒
          code = cat + "\n"
          new CompiledScript {
            def eval(context: ScriptContext): Object = null
            def getEngine: ScriptEngine = OMScripted.this
          }
        case Left(_) ⇒
          code = ""
          throw firstError map {
            case (pos, msg) ⇒ new ScriptException(msg, script, pos.line, pos.column)
          } getOrElse new ScriptException("compile-time error")
      }
    }

  /* Compile with the default context. All references must be resolvable. */
  @throws[ScriptException]
  def compile(script: String): CompiledScript = compile(script, context)

  @throws[ScriptException]
  def compile(reader: Reader): CompiledScript = compile(stringFromReader(reader), context)

  /* Compile and evaluate with the given context. */
  @throws[ScriptException]
  def eval(script: String, context: ScriptContext): Object = compile(script, context).eval(context)

  @throws[ScriptException]
  def eval(reader: Reader, context: ScriptContext): Object = compile(stringFromReader(reader), context).eval(context)

  private class WrappedRequest(val req: intp.Request) extends CompiledScript {
    var first = true

    private def evalEither(r: intp.Request, ctx: ScriptContext) = {
      if (ctx.getWriter == null && ctx.getErrorWriter == null && ctx.getReader == null) r.lineRep.evalEither
      else {
        val closeables = Array.ofDim[Closeable](2)
        val w = if (ctx.getWriter == null) Console.out else {
          val v = new WriterOutputStream(ctx.getWriter)
          closeables(0) = v
          v
        }
        val e = if (ctx.getErrorWriter == null) Console.err else {
          val v = new WriterOutputStream(ctx.getErrorWriter)
          closeables(1) = v
          v
        }
        val in = if (ctx.getReader == null) Console.in else ctx.getReader
        try {
          Console.withOut(w) {
            Console.withErr(e) {
              Console.withIn(in) {
                r.lineRep.evalEither
              }
            }
          }
        }
        finally {
          closeables foreach (c ⇒ if (c != null) c.close())
        }
      }
    }

    /* First time, cause lazy evaluation of a memoized result.
     * Subsequently, instantiate a new object for evaluation.
     * Per the API: Checked exception types thrown by underlying scripting implementations
     * must be wrapped in instances of ScriptException.
     */
    @throws[ScriptException]
    override def eval(context: ScriptContext) = withScriptContext(context) {
      if (first) {
        val result = evalEither(req, context) match {
          case Left(e: RuntimeException) ⇒ throw e
          case Left(e: Exception)        ⇒ throw new ScriptException(e)
          case Left(e)                   ⇒ throw e
          case Right(result)             ⇒ result.asInstanceOf[Object]
        }
        intp recordRequest req
        first = false
        result
      }
      else {
        val defines = req.defines
        if (defines.isEmpty) {
          OMScripted.this.eval(s"new ${req.lineRep.readPath}")
          intp recordRequest duplicate(req)
          null
        }
        else {
          val instance = s"val $$INSTANCE = new ${req.lineRep.readPath};"
          val newline = (defines map (s ⇒ s"val ${s.name} = $$INSTANCE${req.accessPath}.${s.name}")).mkString(instance, ";", ";")
          //val newreq = intp.requestFromLine(newline).right.get
          val newreq = OMScripted.call[IMain, Either[IR.Result, intp.Request]](intp, "requestFromLine", Vector(newline)).right.get
          val ok = newreq.compile

          val result = evalEither(newreq, context) match {
            case Left(e: RuntimeException) ⇒ throw e
            case Left(e: Exception)        ⇒ throw new ScriptException(e)
            case Left(e)                   ⇒ throw e
            case Right(result)             ⇒ intp recordRequest newreq; result.asInstanceOf[Object]
          }
          result
        }
      }
    }

    def duplicate(req: intp.Request) = new intp.Request(req.line, req.trees)

    def getEngine: ScriptEngine = OMScripted.this
  }

  override def getFactory: ScriptEngineFactory = factory
}
