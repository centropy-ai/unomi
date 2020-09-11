var opts = {
    scope: 'web-1e89gMA7K9pdl4lnwUJLToaQhnB',
    url: 'http://localhost:8000',
    writeKey: '1e89gM8JEdb58WtnLnclquehGnQ'
};

!function(){var follower=window.follower=window.follower||[];if(!follower.initialize)if(follower.invoked)window.console&&console.error&&console.error("Segment snippet included twice.");else{follower.invoked=!0;follower.methods=["trackSubmit","trackClick","trackLink","trackForm","pageview","personalize","identify","initialize","reset","group","track","ready","alias","debug","page","once","off","on","addSourceMiddleware","addIntegrationMiddleware","setAnonymousId","addDestinationMiddleware"];follower.factory=function(t){return function(){var e=Array.prototype.slice.call(arguments);e.unshift(t);follower.push(e);return follower}};for(var t=0;t<follower.methods.length;t++){var e=follower.methods[t];follower[e]=follower.factory(e)}follower.load=function(t,e){var n=document.createElement("script");n.type="text/javascript";n.async=!0;n.src="//localhost:8000/miner.min.js";var a=document.getElementsByTagName("script")[0];a.parentNode.insertBefore(n,a);follower._loadOptions=e};follower.SNIPPET_VERSION="0.1.0";
    follower.load();
    follower.initialize({"Prime Data": opts})
    follower.track("Play Game")
}}();
