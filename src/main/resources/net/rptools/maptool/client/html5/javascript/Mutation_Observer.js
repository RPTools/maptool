const maptool_observer = new MutationObserver(
    function(mutations, observer) {
	for(let mutation of mutations) {
	    if (mutation.type === 'childList') {
		for (let i = 0; i < mutation.addedNodes.length; i++) {
		    MapTool.handleAddedNode(mutation.addedNodes[i]);
		}
	    }
	}
    }
);
maptool_observer.observe(document.documentElement, { attributes: false, characterData: false, childList: true, subtree: true });
