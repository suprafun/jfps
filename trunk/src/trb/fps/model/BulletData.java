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

package trb.fps.model;

import trb.jsg.util.Vec3;


/**
 *
 * @author tomrbryn
 */
public class BulletData {

    public boolean alive = false;
    public int shooterPlayerIdx = 0;
    public int bulletType = 0;
    public float[] startPosition = new float[3];
    public float[] startDirection = new float[3];
    public long spawnTime = 0;
    //public Vec3 hitPosition = new Vec3();
    //public long hitTime = 0;

    public void setStartPosition(Vec3 p) {
        System.arraycopy(p.toFloats(), 0, startPosition, 0, 3);
    }

    public void setStartDirection(Vec3 p) {
        System.arraycopy(p.toFloats(), 0, startDirection, 0, 3);
    }
}
