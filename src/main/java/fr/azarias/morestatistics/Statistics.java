/*
 * The MIT License
 *
 * Copyright 2017-2018 azarias.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fr.azarias.morestatistics;

import com.google.gson.JsonObject;

/**
 * Enum of all the additionnal statistics
 * these are incremented using spigot's listeners
 */
public enum Statistics {

    AFK_ONE_MINUTE("custom","afk_one_minute"),
    ELYTRA_PROPELS("custom", "elytra_propels"),
    PATH_BLOCK("custom", "path_block"),
    TAME_ENTITY("custom", "tame_entity"),
    XP_RECEIVED("custom", "xp_received"),
    WORDS_SAID("custom", "words_said"),
    LAUNCH_TRIDENT("custom", "launch_trident"),
    STRIPPED_WOOD("custom", "stripped_wood"),
    SHEAR_SHEEP("custom", "shear_sheep");

    Statistics(String type, String name){
        this.type = type;
        this.name = name;
    }

    public Statistics addToJson(JsonObject obj, long increment){
        String action = "minecraft:" + type,
                mName = "minecraft:" + name;
        if(!obj.has(action)){
            obj.add(action, new JsonObject());
        }
        JsonObject target = obj.getAsJsonObject(action);
        if(!target.has(mName)){
            target.addProperty(mName, 0L);
        }
        Long actualValue = target.get(mName).getAsLong();
        target.addProperty(mName, actualValue + increment);
        return this;
    }
    
    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }

    private final String type, name;


}