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
    [part~='body-cell'] {
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

const whenRendered = (grid) => {
  return new Promise((resolve) => {
    let readyTimer;
    let endTime;
    const listener = (e) => {
      if (e.animationName === 'content-ready' || e.propertyName === 'opacity') {
        endTime = performance.now();
        readyTimer && clearTimeout(readyTimer);
        if (!grid.loading) {
          readyTimer = setTimeout(() => {
            grid.$.scroller.removeEventListener('animationstart', listener);
            grid.removeEventListener('animationstart', listener);
            grid.removeEventListener('transitionstart', listener);
            resolve(performance.now() - endTime);
            // The timeout needs to be large enough so everything gets rendered
            // but small enough so the tests won't take forever. This resolves with
            // the idle time between the last invocation and timeout.
          }, 1000);
        }
      }
    };

    grid.$.scroller.addEventListener('animationstart', listener);
    grid.addEventListener('animationstart', listener);
    grid.addEventListener('transitionstart', listener);
  });
};

const reportResult = (result) => {
  // @ts-ignore
  window.tachometerResult = result;

  const resultDiv = document.createElement('div');
  resultDiv.textContent = `Result: ${result}`;
  resultDiv.style.position = 'fixed';
  resultDiv.style.left = resultDiv.style.top = '0';
  document.body.appendChild(resultDiv);
};

let start = 0;

// @ts-ignore
window.startWhenReady = () => {
  return Promise.all([
    customElements.whenDefined('vaadin-grid'),
    customElements.whenDefined('vaadin-grid-column'),
  ]).then(() => (start = performance.now()));
};

// @ts-ignore
window.startWhenRendered = (grid) => {
  return whenRendered(grid).then(() => {
    start = performance.now();
  });
};

// @ts-ignore
window.whenRendered = whenRendered;

// @ts-ignore
window.measureRender = (grid) => {
  whenRendered(grid).then((idleTime) => {
    reportResult(performance.now() - start - idleTime);
  });
};

const SCROLL_TIME = 10000;
const WARMUP_TIME = 1000;

const scroll = (grid, frames, startTime, previousTime, deltaXMultiplier, deltaYMultiplier) => {
  const now = performance.now();
  const e = new CustomEvent('wheel', { bubbles: true, cancelable: true });

  // @ts-ignore
  e.deltaX = (now - previousTime) * deltaXMultiplier;
  // @ts-ignore
  e.deltaY = (now - previousTime) * deltaYMultiplier;

  grid.dispatchEvent(e);

  // Switch horizontal scroll direction in case end was reached
  if (deltaXMultiplier) {
    const overflow = grid.getAttribute('overflow');
    if (deltaXMultiplier === 1 && !overflow.includes('right')) {
      deltaXMultiplier = -1;
    } else if (deltaXMultiplier === -1 && !overflow.includes('left')) {
      deltaXMultiplier = 1;
    }
  }

  if (now < startTime + WARMUP_TIME + SCROLL_TIME) {
    requestAnimationFrame(() =>
      scroll(
        grid,
        now > startTime + WARMUP_TIME ? frames + 1 : 0,
        startTime,
        now,
        deltaXMultiplier,
        deltaYMultiplier
      )
    );
  } else {
    const frameTime = SCROLL_TIME / frames;
    reportResult(frameTime);
  }
};

// @ts-ignore
window.measureScrollFrameTime = (grid, horizontal) => {
  scroll(
    grid,
    0,
    performance.now(),
    performance.now(),
    horizontal ? 1 : 0,
    horizontal ? 0 : 1
  );
};
