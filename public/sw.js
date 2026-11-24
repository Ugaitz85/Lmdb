const CACHE = 'nuika-video-v5.0.1';
const root = self.registration.scope;
const assets = ['./','index.html','manifest.json','nuika-video-creator-v4.html'].map(p=>new URL(p,root).href);
self.addEventListener('install', e=>e.waitUntil(caches.open(CACHE).then(c=>c.addAll(assets))));
self.addEventListener('activate', e=>e.waitUntil((async()=>{
 for(const k of await caches.keys()) if((k==='nuika-v5'||k.startsWith('nuika-video-'))&&k!==CACHE) await caches.delete(k);
 await self.clients.claim();
})()));
self.addEventListener('fetch', e=>{
 const r=e.request;
 if(r.method!=='GET'||!r.url.startsWith(root)||(!assets.includes(r.url)&&r.mode!=='navigate')) return;
 e.respondWith((async()=>{
 const c=await caches.open(CACHE);
 try {const res=await fetch(r); if(res.ok&&assets.includes(r.url)&&!res.redirected) await c.put(r,res.clone()); if(res.status<500)return res;}catch{}
 return await c.match(r)||(r.mode==='navigate'&&await c.match(new URL('index.html',root).href))||new Response('Sin conexión',{status:503});
 })());
});
