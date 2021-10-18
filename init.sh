echo "- exporting database username and password -"
export APP_DATASOURCE_USERNAME=$(cat "$APP_DATASOURCE_USERNAME_PATH" 2> /dev/null || echo $APP_DATASOURCE_USERNAME)
export APP_DATASOURCE_PASSWORD=$(cat "$APP_DATASOURCE_PASSWORD_PATH" 2> /dev/null || echo $APP_DATASOURCE_PASSWORD)
echo "- exported APP_DATASOURCE_USERNAME og APP_DATASOURCE_PASSWORD for familie-ba-infotrygd "

if [ -z "$APP_DATASOURCE_USERNAME" ]
then
  echo "username har ikke verdi"
fi

if [ -z "$APP_DATASOURCE_PASSWORD" ]
then
  echo "password har ikke verdi"
fi