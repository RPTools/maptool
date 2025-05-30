HTMLFormElement.prototype.submit = function () {
  this.dispatchEvent(new Event('submit'));
};
