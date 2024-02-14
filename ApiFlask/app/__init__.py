from flask import Flask
from werkzeug.middleware.proxy_fix import ProxyFix  # Nuevo import
from flask_dance.contrib.facebook import make_facebook_blueprint
from . import chatbot, api, facebookauth
from .chatbot import socketio
import os

app = Flask(__name__)
#ocultar proxy
#app.wsgi_app = ProxyFix(app.wsgi_app, x_for=1, x_proto=1)  
socketio.init_app(app)

#denegar http
app.secret_key = 'hgfdshgfd'

app.config["OAUTHLIB_INSECURE_TRANSPORT"] = False
app.config["OAUTHLIB_RELAX_TOKEN_SCOPE"] = True
app.config["PREFER_SECURE_URLS"] = True

facebook_blueprint = make_facebook_blueprint(
    client_id='tvbgyuhnijmoik,pl',
    client_secret='nhgjivofkmdgnjivfk',
    scope=['pages_show_list', 'pages_messaging'],
    redirect_to='facebookauth.manage_pages'
)

app.register_blueprint(facebook_blueprint, url_prefix="/login")

app.register_blueprint(facebookauth.bp)
app.register_blueprint(api.bp)
app.register_blueprint(chatbot.bp)

#os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = '1'

if __name__ == "__main__":

    socketio.run(app)