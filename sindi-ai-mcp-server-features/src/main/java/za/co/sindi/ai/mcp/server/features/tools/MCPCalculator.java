package za.co.sindi.ai.mcp.server.features.tools;

import za.co.sindi.ai.mcp.server.spi.Argument;
import za.co.sindi.ai.mcp.server.spi.Tool;

public class MCPCalculator {

    @Tool(description = "Performs basic arithmetic operations (add, subtract, multiply, divide)")
    public String operation(
            @Argument(description = "The operation to perform - add, subtract, multiply, divide") String operation, 
            @Argument(description = "The first operand") double a, 
            @Argument(description = "The second operand") double b) {
        return processOperation(a, b, operation);
    }

    private String processOperation(double a, double b, String operation) {
        return switch (operation.toLowerCase()) {
            case "add" -> String.valueOf(a + b);
            case "subtract" -> String.valueOf(a - b);
            case "multiply" -> String.valueOf(a * b);
            case "divide" -> {
                if (b == 0) {
                    yield "Division by zero is not allowed!";
                }
                yield String.valueOf(a / b);
            }
            default -> "Unknown operation!";
        };
    }
}
