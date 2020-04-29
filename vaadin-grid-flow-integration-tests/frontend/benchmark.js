import {
  registerStyles,
  css,
} from '@vaadin/vaadin-themable-mixin/register-styles.js';

var style = document.createElement('style');
style.type = 'text/css';
style.appendChild(
  document.createTextNode(`
  @keyframes content-ready {
    from {
      opacity: 1;
    }
    to {
      opacity: 1;
    }
  }

  vaadin-grid-tree-toggle[leaf] {
    transition: opacity 1s;
    opacity: 0.9;
  }
`)
);
document.head.appendChild(style);

registerStyles(
  'vaadin-grid',
  css`
    [part~='row'] {
      height: 50px;
    }

    @keyframes content-ready {
      to {
        opacity: 1;
      }
    }

    [part~='cell'] {
      animation: content-ready 1s;
    }

    :host {
      transition: opacity 1s;
    }

    :host[loading] {
      opacity: 0.9;
    }
  `
);

registerStyles(
  'vaadin-*',
  css`
    @keyframes content-ready {
      to {
        opacity: 1;
      }
    }

    :host {
      animation: content-ready 1s;
    }
  `
);

let start = 0;

// @ts-ignore
window.startWhenReady = () => {
  return Promise.all([
    customElements.whenDefined('vaadin-grid'),
    customElements.whenDefined('vaadin-grid-column'),
  ]).then(() => (start = performance.now()));
};

// @ts-ignore
window.measureRender = (grid) => {
  let endTime;
  let readyTimer;
  const listener = (e) => {
    if (e.animationName === 'content-ready' || e.propertyName === 'opacity') {
      endTime = performance.now();
      readyTimer && clearTimeout(readyTimer);
      // @ts-ignore
      if (!grid.loading) {
        readyTimer = setTimeout(() => {
          // @ts-ignore
          window.tachometerResult = endTime - start;
          // TODO: This needs to be large enough so everything gets rendered
          // but small enough so the tests won't take forever
        }, 1000);
      }
    }
  };

  grid.$.scroller.addEventListener('animationstart', listener);
  grid.addEventListener('animationstart', listener);
  grid.addEventListener('transitionstart', listener);
};

const SCROLL_TIME = 10000;
const WARMUP_TIME = 1000;

const scroll = (grid, frames, startTime, previousTime) => {
  const now = performance.now();
  const e = new CustomEvent('wheel', { bubbles: true, cancelable: true });
  // @ts-ignore
  e.deltaY = now - previousTime;
  grid.dispatchEvent(e);

  if (now < startTime + WARMUP_TIME + SCROLL_TIME) {
    requestAnimationFrame(() =>
      scroll(
        grid,
        now > startTime + WARMUP_TIME ? frames + 1 : 0,
        startTime,
        now
      )
    );
  } else {
    const frameTime = SCROLL_TIME / frames;
    // @ts-ignore
    window.tachometerResult = frameTime;
  }
};

// @ts-ignore
window.measureScrollFrameTime = (grid) => {
  scroll(grid, 0, performance.now(), performance.now());
};
