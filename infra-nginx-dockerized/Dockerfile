FROM nginx:latest
RUN apt-get update && apt-get install -y gettext-base

# nginx template
COPY nginx.conf.template /etc/nginx/nginx.conf.template

# static files
COPY static /usr/share/nginx/static
RUN chmod -R 755 /usr/share/nginx/static

RUN mkdir -p /var/log/nginx /tmp/nginx_cache
CMD envsubst ' \
    $HELLOWORLD_PORT \
    $DOCUMENTATION_PORT \
    $ACCOUNTS_PORT \
    $MODULES_PORT \
    ' \
    < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf && \
    nginx -g 'daemon off;'
