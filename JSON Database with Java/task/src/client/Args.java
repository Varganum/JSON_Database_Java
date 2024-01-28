package client;

import com.beust.jcommander.Parameter;

public class Args {

    /* The code from JCommander tutorial example

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = { "-log", "-verbose" }, description = "Level of verbosity")
    private Integer verbose = 1;

    @Parameter(names = "-groups", description = "Comma-separated list of group names to be run")
    private String groups;

    @Parameter(names = "-debug", description = "Debug mode")
    private boolean debug = false;

    private Integer setterParameter;
    @Parameter(names = "-setterParameter", description = "A parameter annotation on a setter method")
    public void setParameter(Integer value) {
        this.setterParameter = value;
    }
    */

    @Parameter(names = "-t", description = "The type of the request (set, get, or delete)")
    public String requestType;

    @Parameter(names = "-k", description = "The index of the cell")
    public String cellIndex;

    @Parameter(names = "-v", description = "The value to save in the database: you only need it in case of a 'set' request")
    public String valueToSave;

    @Parameter(names = "-in", description = "The file name consisting a request to server")
    public String requestFile;

}
