package org.tomdang.entityai.core;

public record AiVector(double x, double y, double z) {
	public AiVector {
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
			throw new IllegalArgumentException("AI vector must be finite");
	}
	public AiVector add(AiVector other) { return new AiVector(x + other.x, y + other.y, z + other.z); }
	public AiVector subtract(AiVector other) { return new AiVector(x - other.x, y - other.y, z - other.z); }
	public AiVector multiply(double amount) { return new AiVector(x * amount, y * amount, z * amount); }
	public double distanceSquared(AiVector other) { double dx=x-other.x,dy=y-other.y,dz=z-other.z; return dx*dx+dy*dy+dz*dz; }
	public double lengthSquared() { return x*x+y*y+z*z; }
	public AiVector normalized() { double length=Math.sqrt(lengthSquared()); return length < 1.0e-8 ? new AiVector(0,0,0) : multiply(1.0/length); }
}
