/*
 * GlueStateProvider.java
 * 
 * Created on Sep 25, 2007, 12:51:57 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
Copyright 2007 Brian Tanner
brian@tannerpages.com
http://brian.tannerpages.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

  
package rlVizLib.visualization.interfaces;

import rlVizLib.general.TinyGlue;

/**
 *
 * @author btanner
 */
public interface GlueStateProvider {
    public TinyGlue getTheGlueState();
}
