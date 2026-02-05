package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;

import za.co.sindi.ai.mcp.schema.Icon.Theme;

/**
 * @author Buhake Sindi
 * @since 11 January 2026
 */
class IconInfo implements Serializable {

	private String src;
	private String mimeType;
	private String[] sizes;
	private Theme theme;
	/**
	 * @param src
	 * @param mimeType
	 * @param sizes
	 * @param theme
	 */
	public IconInfo(String src, String mimeType, String[] sizes, Theme theme) {
		super();
		this.src = src;
		this.mimeType = mimeType;
		this.sizes = sizes;
		this.theme = theme;
	}
	
	/**
	 * @return the src
	 */
	public String getSrc() {
		return src;
	}
	
	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}
	
	/**
	 * @return the sizes
	 */
	public String[] getSizes() {
		return sizes;
	}
	
	/**
	 * @return the theme
	 */
	public Theme getTheme() {
		return theme;
	}
}