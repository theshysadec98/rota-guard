const stateGroups = document.querySelectorAll(".state-group");

const setState = (group, stateName) => {
  const states = group.querySelectorAll(".state");
  states.forEach((state) => state.classList.remove("is-active"));
  const next = group.querySelector(`.state-${stateName}`);
  if (next) {
    next.classList.add("is-active");
  }
};

document.querySelectorAll("[data-state-group]").forEach((button) => {
  button.addEventListener("click", () => {
    const groupName = button.dataset.stateGroup;
    const stateName = button.dataset.state;
    const group = document.querySelector(`.state-group[data-state-group='${groupName}']`);
    if (group) {
      setState(group, stateName);
    }
  });
});

document.querySelectorAll("[data-action='toggle-theme']").forEach((button) => {
  button.addEventListener("click", () => {
    document.body.classList.toggle("theme-light");
    document.body.classList.toggle("theme-dark");
  });
});

const openOverlay = (element) => {
  if (element) {
    element.classList.add("is-open");
    element.setAttribute("aria-hidden", "false");
  }
};

const closeOverlay = (element) => {
  if (element) {
    element.classList.remove("is-open");
    element.setAttribute("aria-hidden", "true");
  }
};

document.querySelectorAll("[data-action='open-modal']").forEach((button) => {
  button.addEventListener("click", () => {
    const targetId = button.dataset.target;
    const modal = document.getElementById(targetId);
    openOverlay(modal);
  });
});

document.querySelectorAll("[data-action='open-drawer']").forEach((button) => {
  button.addEventListener("click", () => {
    const targetId = button.dataset.target;
    const drawer = document.getElementById(targetId);
    openOverlay(drawer);
  });
});

document.querySelectorAll("[data-action='close-modal']").forEach((button) => {
  button.addEventListener("click", () => {
    const modal = button.closest(".modal");
    closeOverlay(modal);
  });
});

document.querySelectorAll("[data-action='close-drawer']").forEach((button) => {
  button.addEventListener("click", () => {
    const drawer = button.closest(".drawer");
    closeOverlay(drawer);
  });
});

document.querySelectorAll("[data-action='open-popover']").forEach((button) => {
  button.addEventListener("click", () => {
    const popoverId = button.dataset.target;
    const popover = document.getElementById(popoverId);
    openOverlay(popover);
  });
});

document.querySelectorAll(".popover").forEach((popover) => {
  popover.addEventListener("click", (event) => {
    if (event.target === popover) {
      closeOverlay(popover);
    }
  });
});

document.querySelectorAll(".modal").forEach((modal) => {
  modal.addEventListener("click", (event) => {
    if (event.target === modal) {
      closeOverlay(modal);
    }
  });
});

document.querySelectorAll(".drawer").forEach((drawer) => {
  drawer.addEventListener("click", (event) => {
    if (event.target === drawer) {
      closeOverlay(drawer);
    }
  });
});

document.querySelectorAll(".toggle-btn").forEach((button) => {
  button.addEventListener("click", () => {
    document.querySelectorAll(".toggle-btn").forEach((item) => item.classList.remove("is-active"));
    button.classList.add("is-active");
  });
});
