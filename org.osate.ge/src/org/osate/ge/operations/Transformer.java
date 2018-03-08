package org.osate.ge.operations;

import java.util.function.Function;

public interface Transformer<PrevReturnType, ReturnType>
		extends Function<PrevReturnType, StepResult<ReturnType>> {
}
