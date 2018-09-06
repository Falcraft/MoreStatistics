/*
 * The MIT License
 *
 * Copyright 2017 azarias.
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

import java.util.Map;
import org.json.simple.JSONObject;

/**
 *
 * @author azarias
 */
public class PlayerStat {

    private final Map<String, Long> mStats;

    public PlayerStat() {
        mStats = new JSONObject();
    }

    public PlayerStat(JSONObject from) {
        mStats = from;
    }

    public void addStat(String statName, int value) {
        if (!mStats.containsKey(statName)) {
            mStats.put(statName, 0L);
        }

        Integer i = value;
        
        long actualValue = mStats.get(statName);
        mStats.put(statName, actualValue + i.longValue());
    }

    public String toJSON() {
        return JSONObject.toJSONString(mStats);
    }
}
