/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openmole.ui.ide.workflow.implementation.paint

import org.netbeans.api.visual.widget.ImageWidget
import org.netbeans.api.visual.widget.Scene
import org.openmole.ui.ide.workflow.implementation.CapsuleViewUI

class SlotWidget(scene: Scene, capsule: CapsuleViewUI[_]) extends ImageWidget(scene){}

//SlotWidget extends ImageWidget{
//
//    private CapsuleViewUI capsuleView;
//
//    public SlotWidget(Scene scene, CapsuleViewUI capsuleView) {
//        super(scene);
//        this.capsuleView = capsuleView;
//    }
//
//    public CapsuleViewUI getCapsuleView() {
//        return capsuleView;
//    }