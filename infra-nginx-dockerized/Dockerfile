FROM nginx:latest
RUN apt-get update && apt-get install -y gettext-base
COPY nginx.conf.template /etc/nginx/nginx.conf.template
RUN mkdir -p /var/log/nginx /tmp/nginx_cache
CMD envsubst ' \
    $HELLOWORLD_PORT \
    $DOCUMENTATION_PORT \
    $ACCOUNTS_PORT \
    $MODULES_PORT \
    ' \
    < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf && \
    nginx -g 'daemon off;'
