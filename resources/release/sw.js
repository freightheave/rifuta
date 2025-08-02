// Establish a cache name
const cacheName = 'rifuta-cache';

const appShellFiles = [
  './',
  './index.html',
  './styles.css',
  './app-js/main.js',
  './app-js/manifest.edn'
];

self.addEventListener('install', (e) => {
  e.waitUntil(
    caches.open(cacheName).then((cache) => {
      return cache.addAll(appShellFiles);
    })
  );
});

self.addEventListener('fetch', (event) => {
    event.respondWith(caches.open(cacheName).then((cache) => {
      return cache.match(event.request).then((cachedResponse) => {
        const fetchedResponse = fetch(event.request).then((networkResponse) => {
          cache.put(event.request, networkResponse.clone());

          return networkResponse;
        });

        return cachedResponse || fetchedResponse;
      });
    }));
});