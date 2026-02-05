package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;
import java.time.OffsetDateTime;

import za.co.sindi.ai.mcp.schema.Role;

/**
 * @author Buhake Sindi
 * @since 11 January 2026
 */
class AnnotationsInfo implements Serializable {
	
	private Role[] audience;
	private Double priority;
	private OffsetDateTime lastModified;
	
	/**
	 * @param audience
	 */
	public AnnotationsInfo(Role[] audience) {
		this(audience, null, null);
	}

	/**
	 * @param audience
	 * @param priority
	 * @param lastModified
	 */
	public AnnotationsInfo(Role[] audience, Double priority, OffsetDateTime lastModified) {
		super();
		this.audience = audience;
		this.priority = priority;
		this.lastModified = lastModified;
	}

	/**
	 * @return the audience
	 */
	public Role[] getAudience() {
		return audience;
	}

	/**
	 * @return the priority
	 */
	public Double getPriority() {
		return priority;
	}

	/**
	 * @return the lastModified
	 */
	public OffsetDateTime getLastModified() {
		return lastModified;
	}
}