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

package trb.fps;

import trb.jsg.util.Vec3;

/**
 *
 * @author tomrbryn
 */
public class CollisionInfo {

    public enum Type {None, Player, World};

    public Type type = Type.None;
    public Vec3 intersection = new Vec3();
    public int playerIdx = -1;

    public CollisionInfo() {

    }

    public CollisionInfo(Type type, Vec3 intersection, int playerIdx) {
        this.type = type;
        this.intersection.set(intersection);
        this.playerIdx = playerIdx;
    }
}
