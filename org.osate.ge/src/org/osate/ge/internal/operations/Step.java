package org.osate.ge.internal.operations;

public interface Step<ResultUserType> {
	Step<?> getNextStep();
}
