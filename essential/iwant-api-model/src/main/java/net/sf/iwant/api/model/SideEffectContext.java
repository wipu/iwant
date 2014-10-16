package net.sf.iwant.api.model;

import java.io.File;
import java.io.OutputStream;

public interface SideEffectContext {

	WsInfo wsInfo();

	File wsRoot();

	OutputStream err();

	TargetEvaluationContext targetEvaluationContext();

	File iwantAsPath(Target target);

}
