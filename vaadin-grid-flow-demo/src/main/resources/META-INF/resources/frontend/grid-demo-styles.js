const $_documentContainer = document.createElement('template');

$_documentContainer.innerHTML = `<dom-module id="my-grid-theme" theme-for="vaadin-grid">
    <template>
        <style>
            [part~="cell"].subscriber {
                background: rgb(245, 245, 255);
            }
            .minor {
                color: red;
                font-weight: bold;
            }
        </style>
    </template>
</dom-module>`;

document.head.appendChild($_documentContainer.content);

/*
  ~ Copyright 2000-2018 Vaadin Ltd.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy of
  ~ the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  ~ WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  ~ License for the specific language governing permissions and limitations under
  ~ the License.
  */
/*
  FIXME(polymer-modulizer): the above comments were extracted
  from HTML and may be out of place here. Review them and
  then delete this comment!
*/
;