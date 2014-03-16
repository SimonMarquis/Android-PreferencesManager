package fr.simon.marquis.preferencesmanager.model;

/*
 * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.Comparator;
import java.util.Map.Entry;

final class PreferenceComparator implements Comparator<Entry<String, Object>> {

    private PreferenceSortType mType = PreferenceSortType.ALPHANUMERIC;

    public PreferenceComparator(PreferenceSortType type) {
        mType = type;
    }

    @Override
    public int compare(Entry<String, Object> lhs, Entry<String, Object> rhs) {
        if (mType == PreferenceSortType.TYPE_AND_ALPHANUMERIC) {
            String l = lhs == null ? "" : (lhs.getValue() == null ? "" : lhs.getValue().getClass().getName());
            String r = rhs == null ? "" : (rhs.getValue() == null ? "" : rhs.getValue().getClass().getName());
            int res = l.compareToIgnoreCase(r);
            if (res != 0) {
                return res;
            }
        }
        return (lhs == null ? "" : lhs.getKey()).compareToIgnoreCase((rhs == null ? "" : rhs.getKey()));
    }
}