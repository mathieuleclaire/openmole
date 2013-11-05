rmdir /s /q "configuration\org.eclipse.core.runtime"
rmdir /s /q "configuration\org.eclipse.equinox.app"
rmdir /s /q "configuration\org.eclipse.osgi"

start /B dbserver\bin\openmole-dbserver.bat

mkdir "%UserProfile%\.openmole\.tmp"

set ran="%UserProfile%\.openmole\.tmp\%random%"

set PWD=%~dp0

java -Dosgi.locking=none -Dopenmole.location="%PWD%\" -Dosgi.classloader.singleThreadLoads=true -Dosgi.configuration.area=%ran% -splash:splashscreen.png -XX:MaxPermSize=128M -XX:+UseG1GC -Xmx1G  -XX:MaxPermSize=128M -jar ./plugins/org.eclipse.equinox.launcher.jar -cp ./openmole-plugins -gp ./openmole-plugins-gui %*

rmdir /s /q %ran%
