package net.rptools.maptool.model;

public class IsometricHexGrid extends HexGridVertical {
	public IsometricHexGrid(boolean faceEdges, boolean faceVertices) {
		super(faceEdges, faceVertices);
	}

	public void setSize(int size) {
		super.setSize(size);

		setMinorRadius( (double) size / 2 );
		setEdgeLength( (double) size );
		setEdgeProjection( (double) size / 2 );
	}
	
	public boolean isIsometric() {
		return true;
	}
}
