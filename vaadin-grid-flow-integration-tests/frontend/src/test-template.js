/* Dependency resources */
/* Defines the example-template element */
/*
  FIXME(polymer-modulizer): the above comments were extracted
  from HTML and may be out of place here. Review them and
  then delete this comment!
*/
import { PolymerElement } from '@polymer/polymer/polymer-element.js';

import { html } from '@polymer/polymer/lib/utils/html-tag.js';
class TestTemplate extends PolymerElement {
  static get template() {
    return html`
        <div id="container" style="height:20px;"></div>
        <button id="btn">Click me!</button>
`;
  }

  static get is() {
      return 'test-template'
  }
}
customElements.define(TestTemplate.is, TestTemplate);