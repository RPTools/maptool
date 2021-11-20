class XMLHttpRequest {
    constructor() {
	this._req = MapTool.makeXMLHttpRequest(this, window.location.href);
	this.readyState = 0;
	this.response = null;
	this.responseText = null;
	this.responseType = "text";
	this.responseXML = null;
	this.status = 0;
	this.statusText = "";
	return this;
    }
    
    open(method, uri, ...rest) {
	switch (rest.length) {
	case 0:
	    rest.push(true);
	case 1:
	    rest.push(null);
	case 2:
	    rest.push(null);
	default:
	    break;
	    
	}
	
	this._req.open(method, uri, ...rest);
    }

    send(body) {
	if (body === undefined) {
	    body = null;
	}
	this._req.send(body);
    }

    abort() {
	this._req.abort();
    }

    setRequestHeader(key, val) {
	this._req.setRequestHeader(key, val);
    }

    getResponseHeader(key) {
	return this._req.getResponseHeader(key);
    }

    getAllResponseHeaders() {
	return this._req.getAllResponseHeaders();
    }
    
}
