/*
 * HALDEN VR PLATFORM
 *
 * RADIATION MODULE
 *
 * $RCSfile: $
 *
 * Author :
 * Date   :
 * Version: $Revision: $ ($Date: $)
 *
 * (c) 2000-2011 Halden Virtual Reality Centre <http://www.ife.no/vr/>,
 * Institutt for energiteknikk. All rights reserved.
 *
 * This code is the property of Halden VR Centre <vr-info@hrp.no> and may
 * only be used in accordance with the terms of the license agreement
 * granted.
 */

package trb.fps.editor;

import org.lwjgl.input.Mouse;

/**
 *
 * @author tomrbryn
 */
public class InputState {

    public boolean isLeftDown() {
        return Mouse.isButtonDown(0);
    }

    public boolean isMiddleDown() {
        return Mouse.isButtonDown(0);
    }
    public boolean isRightDown() {
        return Mouse.isButtonDown(0);
    }
}
