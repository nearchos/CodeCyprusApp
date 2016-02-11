/*
 * Copyright (c) 2016.
 *
 * This file is part of the Code Cyprus App.
 *
 * The Code Cyprus App is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 *  Code Cyprus App is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Code Cyprus App. If not, see <http://www.gnu.org/licenses/>.
 */

package org.codecyprus.android_client.model;

/**
 * @author Nearchos Paspallis
 * 28/12/13
 */
public enum Answer
{
    INCORRECT("incorrect"),
    UNKNOWN_OR_INCORRECT_LOCATION("unknown or incorrect location"),
    CORRECT_UNFINISHED("correct,unfinished"),
    CORRECT_FINISHED("correct,finished");

    private final String message;

    private Answer(final String message)
    {
        this.message = message;
    }

    @Override
    public String toString()
    {
        return message;
    }
}