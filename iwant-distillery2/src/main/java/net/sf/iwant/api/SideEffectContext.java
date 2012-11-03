package net.sf.iwant.api;

import java.io.File;
import java.io.OutputStream;

public interface SideEffectContext {

	WsInfo wsInfo();

	File wsRoot();

	OutputStream err();

	TargetEvaluationContext targetEvaluationContext();

}
