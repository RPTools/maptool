/*
 * This file is based on a heavily modified version of Ext.js version 2.2 which is licensed under
 * GPL v3.
 * 
 * Ext JS Library 2.2 (highly modified to make sense in our environment)
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
rptools = { version: 1.0 };

/**
 * Copies all the properties of config to obj.
 * @param {Object} obj The receiver of the properties
 * @param {Object} config The source of the properties
 * @param {Object} defaults A different object that will also be applied for default values
 * @return {Object} returns obj
 * @member rptools apply
 * Copied from Ext
 */
rptools.apply = function(o, c, defaults){
    if(defaults){
        // no "this" reference for friendly out of scope calls
        rptools.apply(o, defaults);
    }
    if(o && c && typeof c == 'object'){
        for(var p in c){
            o[p] = c[p];
        }
    }
    return o;
};

(function() { // Create a local scope
	
rptools.apply(rptools, {
	
	/**
     * Copies all the properties of config to obj if they don't already exist.
     * @param {Object} obj The receiver of the properties
     * @param {Object} config The source of the properties
     * @return {Object} returns obj
     */
    applyIf : function(o, c){
        if(o && c){
            for(var p in c){
                if(typeof o[p] == "undefined"){ o[p] = c[p]; }
            }
        }
        return o;
    },
    
    /**
         * Extends one class with another class and optionally overrides members with the passed literal. This class
         * also adds the function "override()" to the class that can be used to override
         * members on an instance.
         * * <p>
         * This function also supports a 2-argument call in which the subclass's constructor is
         * not passed as an argument. In this form, the parameters are as follows:</p><p>
         * <div class="mdetail-params"><ul>
         * <li><code>superclass</code>
         * <div class="sub-desc">The class being extended</div></li>
         * <li><code>overrides</code>
         * <div class="sub-desc">A literal with members which are copied into the subclass's
         * prototype, and are therefore shared among all instances of the new class.<p>
         * This may contain a special member named <tt><b>constructor</b></tt>. This is used
         * to define the constructor of the new class, and is returned. If this property is
         * <i>not</i> specified, a constructor is generated and returned which just calls the
         * superclass's constructor passing on its parameters.</p></div></li>
         * </ul></div></p><p>
         * For example, to create a subclass of the Ext GridPanel:
         * <pre><code>
    MyGridPanel = Ext.extend(Ext.grid.GridPanel, {
        constructor: function(config) {
            // Your preprocessing here
        	MyGridPanel.superclass.constructor.apply(this, arguments);
            // Your postprocessing here
        },

        yourMethod: function() {
            // etc.
        }
    });
</code></pre>
         * </p>
         * @param {Function} subclass The class inheriting the functionality
         * @param {Function} superclass The class being extended
         * @param {Object} overrides (optional) A literal with members which are copied into the subclass's
         * prototype, and are therefore shared between all instances of the new class.
         * @return {Function} The subclass constructor.
         * @method extend
         */
    extend : function(){
        // inline overrides
        var io = function(o){
            for(var m in o){
                this[m] = o[m];
            }
        };
        var oc = Object.prototype.constructor;

        return function(sb, sp, overrides){
            if(typeof sp == 'object'){
                overrides = sp;
                sp = sb;
                sb = overrides.constructor != oc ? overrides.constructor : function(){sp.apply(this, arguments);};
            }
            var F = function(){}, sbp, spp = sp.prototype;
            F.prototype = spp;
            sbp = sb.prototype = new F();
            sbp.constructor=sb;
            sb.superclass=spp;
            if(spp.constructor == oc){
                spp.constructor=sp;
            }
            sb.override = function(o){
                Ext.override(sb, o);
            };
            sbp.override = io;
            Ext.override(sb, overrides);
            sb.extend = function(o){Ext.extend(sb, o);};
            return sb;
        };
    }(),
    
    /**
         * Adds a list of functions to the prototype of an existing class, overwriting any existing methods with the same name.
         * Usage:<pre><code>
Ext.override(MyClass, {
    newMethod1: function(){
        // etc.
    },
    newMethod2: function(foo){
        // etc.
    }
});
 </code></pre>
         * @param {Object} origclass The class to override
         * @param {Object} overrides The list of functions to add to origClass.  This should be specified as an object literal
         * containing one or more methods.
         * @method override
         */
    override : function(origclass, overrides){
        if(overrides){
            var p = origclass.prototype;
            for(var method in overrides){
                p[method] = overrides[method];
            }
        }
    },
    
    /**
         * Creates namespaces to be used for scoping variables and classes so that they are not global.  Usage:
         * <pre><code>
Ext.namespace('Company', 'Company.data');
Company.Widget = function() { ... }
Company.data.CustomStore = function(config) { ... }
</code></pre>
         * @param {String} namespace1
         * @param {String} namespace2
         * @param {String} etc
         * @method namespace
         */
    namespace : function(){
        var a=arguments, o=null, i, j, d, rt;
        for (i=0; i<a.length; ++i) {
            d=a[i].split(".");
            rt = d[0];
            eval('if (typeof ' + rt + ' == "undefined"){' + rt + ' = {};} o = ' + rt + ';');
            for (j=1; j<d.length; ++j) {
                o[d[j]]=o[d[j]] || {};
                o=o[d[j]];
            }
        }
    },
    
    /**
     * Iterates an array calling the passed function with each item, stopping if your function returns false. If the
     * passed array is not really an array, your function is called once with it.
     * The supplied function is called with (Object item, Number index, Array allItems).
     * @param {Array/NodeList/Mixed} array
     * @param {Function} fn
     * @param {Object} scope
     */
    each : function(array, fn, scope){
        if(typeof array.length == "undefined" || typeof array == "string"){
            array = [array];
        }
        for(var i = 0, len = array.length; i < len; i++){
            if(fn.call(scope || array[i], array[i], i, array) === false){ return i; };
        }
    },
    
    /**
     * Utility method for validating that a value is numeric, returning the specified default value if it is not.
     * @param {Mixed} value Should be a number, but any type will be handled appropriately
     * @param {Number} defaultValue The value to return if the original value is non-numeric
     * @return {Number} Value, if numeric, else defaultValue
     */
    num : function(v, defaultValue){
        if(typeof v != 'number'){
            return defaultValue;
        }
        return v;
    },
	
	/**
     * Returns true if the passed value is null, undefined or an empty string (optional).
     * @param {Mixed} value The value to test
     * @param {Boolean} allowBlank (optional) Pass true if an empty string is not considered empty
     * @return {Boolean}
     */
    isEmpty : function(v, allowBlank){
        return v === null || v === undefined || (!allowBlank ? v === '' : false);
    },
	
    value : function(v, defaultValue, allowBlank){
        return Ext.isEmpty(v, allowBlank) ? defaultValue : v;
    },
    
    /**
     * Returns true if the passed object is a JavaScript array, otherwise false.
     * @param {Object} The object to test
     * @return {Boolean}
     */
	isArray : function(v){
		return v && typeof v.length == 'number' && typeof v.splice == 'function';
	},

	/**
     * Returns true if the passed object is a JavaScript date object, otherwise false.
     * @param {Object} The object to test
     * @return {Boolean}
     */
	isDate : function(v){
		return v && typeof v.getFullYear == 'function';
	}
});

rptools.ns = rptools.namespace;

rptools.tokens = rptools_global_tokens;

})(); // End local scope

RP = rptools;
