package net.rptools.maptool.model;

public class IsometricHexGrid extends HexGridVertical {
	
	// This constructor is necessary for deserialisation
	public IsometricHexGrid() {
		super();
	}
	
	public IsometricHexGrid(boolean faceEdges, boolean faceVertices) {
		super(faceEdges, faceVertices);
	}

	@Override
	public void setSize(int size) {
		super.setSize(size);

		setMinorRadius( (double) size / 2 );
		setEdgeLength( (double) size );
		setEdgeProjection( (double) size / 2 );
	}

	@Override
	public boolean isIsometric() {
		return true;
	}
}
