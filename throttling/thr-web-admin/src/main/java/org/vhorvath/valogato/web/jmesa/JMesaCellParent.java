package org.vhorvath.valogato.web.jmesa;

import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.editor.HtmlCellEditor;

public class JMesaCellParent implements CellEditor {

	private int width;
	
	protected JMesaCellParent(int width) {
		this.width = width;
	}
	
	@Override
	public Object getValue(Object item, String property, int rowcount) {
		Object value = new HtmlCellEditor().getValue(item, property, rowcount);
		HtmlBuilder htmlBuilder = new HtmlBuilder();
		
		htmlBuilder.div().style("width: "+width+"px; word-wrap: break-word").close();
		htmlBuilder.append(value);
		htmlBuilder.divEnd();
		
		return htmlBuilder.toString();
	}

}
