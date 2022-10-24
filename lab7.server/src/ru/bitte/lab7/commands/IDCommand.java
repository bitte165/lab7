package ru.bitte.lab7.commands;

import ru.bitte.lab7.route.Route;

/**
 * Interface used for commands that receive an ID argument. The reason for this being an interface is that both element
 * commands and otherwise no-argument commands can have ID passed to them in certain command implementations.
 * @see RemoveByIDCommand
 * @see UpdateCommand
 */
public interface IDCommand {
    /**
     * Sets the ID argument with which the command will run the next time
     * @param id the integer value corresponding to an ID of a {@link Route} object
     */
    void passID(int id);

    /**
     * Returns the current ID argument of the command
     * @return the integer value corresponding to an ID
     */
    int getID();
}
