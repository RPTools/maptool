class XMLHttpRequest {
    constructor() {
	this._req = MapTool.makeXMLHttpRequest(this, window.location.href);
	this.response = null;
	this.responseType = "text";
	this.onreadystatechange = function(){};
	return this;
    }

    get responseText() {
	if (this.responseType == "text" || this.responseType == "") {
	    return this.response;
	}
	throw new DOMException("Failed to read the 'responseText' property from 'XMLHttpRequest': The value is only accessible if the object's 'responseType' is '' or 'text'");
    }

    getStatus() {
	return this._req.getStatus();
    }

    get statusText() {
	let s = this._req.getStatus();
	return s.substring(s.indexOf(' ') + 1);
    }

    get readyState() {
	return this._req.getReadyState();
    }

    set responseType(typ) {
	this._req.setResponseType(typ);
    }

    get responseType() {
	return this._req.getResponseType();
    }

    _makeBlob(s) {
	this.response = new Blob([s]);
    }

    _makeArrayBuffer(s) {
	let buf = new ArrayBuffer(s.length * 2);
	let bufView = new Uint16Array(buf);
	for (let i = 0, len = s.length; i < len; i++) {
	    bufView[i] = s.charCodeAt(i);
	}
	this.resposne = buf;
    }

    _makeDocument(s) {
	let parser = new DOMParser();
	this.response = parser.parseFromString(s, "text/html");
    }

    _makeJson(s) {
	this.response = JSON.parse(s);
    }

    _makeText(s) {
	this.response = s;
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

    _warnAsync() {
	console.log("WARNING: Synchronous XMLHttpRequests can negatively degrade the user experience and are discouraged!");
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


async function fetch(target, optionObject) {
    let request;
    if (target instanceof Request) {
	request = target;
    }
    else { // target is a URI, extra options are in the second argument
	if (optionObject) {
	    request = new Request(target, optionObject);
	}
	else {
	    request = new Request(target);
	}
    }

    let x = new XMLHttpRequest();
    x.open(request.method, request.url);
    for (let header of request.headers) {
	x.setRequestHeader(header[0], header[1])
    }
    let body = await request.text();
    

    let _resolve;
    let _reject;
    let p = new Promise(
	(resolve, reject)=> {
	    _resolve = resolve;
	    _reject = reject;
	}
    );

    x.onreadystatechange = () => {
	try {
	    if (x.readyState == 4) {
		let status = parseInt(x.getStatus().trim().split(' ')[0]);
		let jheaders = {};
		x._req._getResponseHeaders(jheaders);
		_resolve(new Response(x.response, {status: status, statusText: x.statusText, headers: jheaders}));
	    }
	}
	catch (e) {
	    console.log("135: " + e)
	    console.log(e.stack)
	}
    };
    try {
	x.send(body);
    }
    catch (e) {
	_reject(e);
    }
    return p;


}
