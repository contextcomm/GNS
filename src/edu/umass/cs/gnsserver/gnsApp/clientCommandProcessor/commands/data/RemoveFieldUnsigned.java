/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gnsserver.gnsApp.clientCommandProcessor.commands.data;

import edu.umass.cs.gnsserver.gnsApp.clientCommandProcessor.commands.CommandModule;
import edu.umass.cs.gnsserver.gnsApp.clientCommandProcessor.commandSupport.UpdateOperation;
import static edu.umass.cs.gnsserver.gnsApp.clientCommandProcessor.commandSupport.GnsProtocolDefs.*;

/**
 *
 * @author westy
 */
public class RemoveFieldUnsigned extends AbstractUpdate {

  /**
   *
   * @param module
   */
  public RemoveFieldUnsigned(CommandModule module) {
    super(module);
  }

  /**
   * Return the update operation.
   * 
   * @return an {@link UpdateOperation}
   */
  @Override
  public UpdateOperation getUpdateOperation() {
    return UpdateOperation.SINGLE_FIELD_REMOVE_FIELD;
  }

  @Override
  public String getCommandName() {
    return REMOVEFIELD;
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUID, FIELD, WRITER};
  }

  @Override
  public String getCommandDescription() {
    return "Removes the key value pair from the GNS for the given guid."
            + " Field must be world writeable as this command does not specify the writer and is not signed.";
    
  }
}