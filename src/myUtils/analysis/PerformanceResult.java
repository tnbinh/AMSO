package myUtils.analysis;

public class PerformanceResult {

	/**
	 * Field declarations
	*/
        private double train;
        private double test;

    public PerformanceResult(double train, double test) {
        this.train = train;
        this.test = test;
    }

	public PerformanceResult() {

	}

    public double getTest() {
        return test;
    }

    public void setTest(double test_err) {
        this.test = test_err;
    }

    public double getTrain() {
        return train;
    }

    public void setTrain(double train_err) {
        this.train = train_err;
    }

    public String toString(){
		return ("Train: " + getTrain() + ", Test: " + getTest());
	}
}





