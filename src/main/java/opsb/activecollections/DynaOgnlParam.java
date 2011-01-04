package opsb.activecollections;

import ognl.Ognl;
import ognl.OgnlException;

public class DynaOgnlParam implements DynaParam {

	private Object root;
	
	private String ognl;
	
	public DynaOgnlParam(Object root, String ognl) {
		this.root = root;
		this.ognl = ognl;
	}
	
	public Object getValue() {
		try {
			return Ognl.getValue(ognl, root);
		} catch (OgnlException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static DynaOgnlParam ognlParam(Object bean, String ognl) {
		return new DynaOgnlParam(bean, ognl);
	}

}
