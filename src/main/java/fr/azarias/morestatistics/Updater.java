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

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;

public class Updater {

    public enum STAT_VERSION{
        VERSION_ONE
    };

    private static ImmutableMap<String, Statistics> versionOneEqui = new ImmutableMap.Builder<String, Statistics>()
            .put("stat.elytraPropels", Statistics.ELYTRA_PROPELS)
            .put("custom.tameEntity", Statistics.TAME_ENTITY)
            .put("stat.pathBlock", Statistics.PATH_BLOCK)
            .put("stat.afkOneMinute", Statistics.AFK_ONE_MINUTE)
            .put("stat.shearSheep", Statistics.SHEAR_SHEEP)
            .build();

    /**
     * From original version to 1.13 statistics version
     */
    private static JsonObject toVersionOne(JsonObject origin){
        JsonObject res = new JsonObject();
        JsonObject stats = new JsonObject();
        res.addProperty("DataVersion", 1);
        res.add("stats", stats);
        origin.entrySet().forEach((set) -> {
                if(versionOneEqui.containsKey(set.getKey())){
                    versionOneEqui.get(set.getKey()).addToJson(stats, set.getValue().getAsLong());
                }
            }
        );
        return res;
    }

    /**
     * Updates the version of the given object to the given version
     */
    public static JsonObject toVersion(STAT_VERSION version, JsonObject origin)
    {
        switch(version){
            case VERSION_ONE:
            return toVersionOne(origin);
            default:
            return origin;// can't translate it
        }
    }

}