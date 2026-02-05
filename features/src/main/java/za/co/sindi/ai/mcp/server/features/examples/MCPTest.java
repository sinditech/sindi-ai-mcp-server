/**
 * 
 */
package za.co.sindi.ai.mcp.server.features.examples;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.sindi.ai.mcp.schema.BooleanSchema;
import za.co.sindi.ai.mcp.schema.ElicitRequest.ElicitRequestFormParameters.RequestedSchema;
import za.co.sindi.ai.mcp.schema.ElicitResult;
import za.co.sindi.ai.mcp.schema.ElicitResult.Action;
import za.co.sindi.ai.mcp.server.spi.ElicitationContext;
import za.co.sindi.ai.mcp.server.spi.Tool;
import za.co.sindi.ai.mcp.server.spi.ToolArgument;

/**
 * 
 */
@ApplicationScoped
public class MCPTest {
	
	private static final Logger LOGGER = Logger.getLogger(MCPTest.class.getName());

	@Inject
	private ElicitationContext elicitationContext;

	@Tool(name = "delete_file", description = "Deletes a file with user confirmation")
	public String deleteFile(
			@ToolArgument(description = "File path") String filePath) throws Exception {

		// Check if file exists
		Path path = Paths.get(filePath);
		if (!Files.exists(path)) {
			return "File not found: " + filePath;
		}

		// Request confirmation via elicitation
		RequestedSchema schema = new RequestedSchema();
		schema.setProperties(new LinkedHashMap<>());
		schema.getProperties().put("confirm", new BooleanSchema());
		schema.getProperties().get("confirm").setDescription("Confirm deletion.");
		schema.setRequired(new String[] { "confirm" });

		ElicitResult result = elicitationContext.elicitInput(String.format(
				"Are you sure you want to delete '%s'? This cannot be undone.",
				filePath), schema).join();
		;

		if (result.getAction() != Action.ACCEPT) {
			return "Deletion cancelled by user.";
		}

		Boolean confirmed = (Boolean) result.getContent().get("confirm");
		if (!confirmed) {
			return "Deletion not confirmed.";
		}

		// Perform deletion
		Files.delete(path);
		return "File deleted successfully: " + filePath;
	}

//	@Tool(
//            name = "process_large_dataset",
//            description = "Process a large dataset with cancellation support"
//        )
//    @Cancellable  // Mark this tool as cancellable
//    public String processLargeDataset(
//        @ToolArgument(description = "Dataset path") String datasetPath,
//        @ToolArgument(description = "Batch size") int batchSize,
//        CancellationContext cancellationContext  // Inject cancellation context
//    ) throws Exception {
//        
//		LOGGER.info(String.format("Starting dataset processing: %s", datasetPath));
//        
//        List<DataRecord> records = loadDataset(datasetPath);
//        int totalBatches = (int) Math.ceil((double) records.size() / batchSize);
//        
//        StringBuilder results = new StringBuilder();
//        
//        for (int i = 0; i < totalBatches; i++) {
//            // Check for cancellation periodically
//            cancellationContext.skipProcessingIfCancelled()
//            
//            int start = i * batchSize;
//            int end = Math.min(start + batchSize, records.size());
//            List<DataRecord> batch = records.subList(start, end);
//            
//            // Process batch
//            String batchResult = processBatch(batch);
//            results.append(batchResult).append("");
//            
//            LOGGER.debug("Processed batch {}/{}", i + 1, totalBatches);
//        }
//        
//        LOGGER.info("Dataset processing completed successfully");
//        return results.toString();
//    }
//
//	private List<DataRecord> loadDataset(String path) {
//		// Load dataset logic
//		return new ArrayList<>();
//	}
//
//	private String processBatch(List<DataRecord> batch) {
//		// Process batch logic
//		try {
//			Thread.sleep(1000); // Simulate processing time
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//			throw new CancellationException("Processing interrupted");
//		}
//		return "Batch processed: " + batch.size() + " records";
//	}
}
