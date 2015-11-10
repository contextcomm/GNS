/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gnsserver.gnsApp.clientCommandProcessor.commands.group;

import static edu.umass.cs.gnsserver.gnsApp.clientCommandProcessor.commandSupport.GnsProtocolDefs.*;
import edu.umass.cs.gnsserver.gnsApp.clientCommandProcessor.commands.CommandModule;

/**
 *
 * @author westy
 */
public class RemoveFromGroupSelf extends RemoveFromGroup {

  /**
   *
   * @param module
   */
  public RemoveFromGroupSelf(CommandModule module) {
    super(module);
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUID, MEMBER, SIGNATURE, SIGNATUREFULLMESSAGE};
  }

  @Override
  public String getCommandName() {
    return REMOVEFROMGROUP;
  }

  @Override
  public String getCommandDescription() {
    return "Removes the member guid from the group specified by guid.";
  }
}