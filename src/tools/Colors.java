package tools;

public class Colors {
	double channel1;
	double channel2;
	double channel3;

	public Colors() {
		this.channel1 = 0;
		this.channel2 = 0;
		this.channel3 = 0;
	}
	public Colors(float[] f) {
		this.channel1 = f[0];
		this.channel2 = f[1];
		this.channel3 = f[2];
	}
	public Colors(double r, double g, double b) {
		this.channel1 = r;
		this.channel2 = g;
		this.channel3 = b;
	}

	public double getChannel(int i) {
		if (i == 0)
			return channel1;
		if (i == 1)
			return channel2;
		return channel3;
	}

	public void setChannel(int i, double value) {
		if (i == 0)
			channel1 = value;
		else if (i == 1)
			channel2 = value;
		else
			channel3 = value;
	}
	public Colors plus(Colors c) {
		this.channel1 += c.channel1;
		this.channel2 += c.channel2;
		this.channel3 += c.channel3;
		return this;
	}
	public Colors product(double n) {
		this.channel1 /= n;
		this.channel2 /= n;
		this.channel3 /= n;
		return this;
	}
	public double minus(Colors c) {
		double value = 0;
		value += Math.abs(this.channel1 - c.channel1);
		value += Math.abs(this.channel2 - c.channel2);
		value += Math.abs(this.channel3 - c.channel3);
		return value;
	}
}
