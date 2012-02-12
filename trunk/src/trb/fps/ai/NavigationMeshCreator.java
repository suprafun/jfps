package trb.fps.ai;

import org.critterai.nmgen.NavmeshGenerator;
import org.critterai.nmgen.TriangleMesh;

public interface NavigationMeshCreator {
	public TriangleMesh create(NavmeshGenerator generator);
}
