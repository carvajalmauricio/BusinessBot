from flask import jsonify, Blueprint, request
import facebook
from langchain import PromptTemplate
import requests
from firebase_admin import db
import os
from langchain.memory import ConversationSummaryMemory
from langchain.chat_models import ChatOpenAI
from langchain.chains import ConversationChain

bp = Blueprint('facebookauth', __name__, url_prefix='/facebookauth')

PAGE_ACCESS_TOKEN = 'hbgvfcdbgvfcd'
VERIFY_TOKEN = 'vybghunjimbghunjim'
def get_api():
    graph = facebook.GraphAPI(PAGE_ACCESS_TOKEN)
    return graph

ref = db.reference('/Users')

def getInf():
    User = ref.child('gOf3SOTfBfSis6jT1L9G4XOzMzH2').get()
    businessName = User['businessName']
    ciudad = User['Business Information']['ciudad']
    contacto = User['Business Information']['contacto']
    direccion = User['Business Information']['direccion']
    horario = User['Business Information']['horario']
    horarioApertura = User['Business Information']['horarioApertura']
    horarioCierre = User['Business Information']['horarioCierre']
    product_list = User['Products']
    informacion = User['Business Information']['informacion']
    # Usa compresión de listas para construir la cadena requerida:
    formatted_string = "-".join(f" nombre: {product['nombre']}, descripcion: {product['descripcion']}, cantidad: {product['cantidad']}, precio: {product['precio']} " for product in product_list)
    template = f"""Este es un mensaje se system: De ahora en adelante eres un agente llamado Bot de ventas, del establecimiento {businessName} que se comunica con el Human por medio de redes sociales, tu tarea es comunicarte con el cliente con el objetivo de concretar una venta e intenta ser concreto y preciso
    , incluye emojis en tus respuestas de vez en cuenado,la informacion del negocio y sus productos disponibles estaran seguidos de '[]' [ciudad: {ciudad},telefono: {contacto},
    direccion: {direccion},horario:{horario}, horariodeapertura: {horarioApertura}, horariodecierre: {horarioCierre},
    informacion del establecimiento:{informacion}, productos disponibles: {formatted_string}] Caulquier informacion que el cliente, pregunte y no la encuentres en este texto, debes decir. "No te entendí, me lo repites?"
    Si el Human dice alguna palabra aluciendo a una despedida, deberás finalizar el chat. IMPORTANTE = Tus respuestas no deben pasar las 20 palabras
        Current conversation:
        {{history}}
        Human:
        {{input}}
        AI:"""
    return template
    
llm = ChatOpenAI(
    openai_api_key='xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    max_tokens=70,
)

SUMMARY_PROMPT = PromptTemplate(
    input_variables=["history", "input"],
    template=getInf()
)

conversation = ConversationChain(
    llm=llm,
    verbose=True,
    prompt=SUMMARY_PROMPT,
    memory=ConversationSummaryMemory(llm=llm)
)

chat_history = []

def modellingOpenAI(input):
    chat_history.append(input)
    response = conversation.predict(input=input)
    # Append the response to the chat_history
    chat_history.append(response)
    # Use the already defined instance of conversation
    return response

@bp.route('/chat')
def chat():
    api = get_api()
    conversations = api.get_object('me/conversations')
    all_messages = []
    for conversation in conversations['data']:
        conversation_id = conversation['id']
        args = {'fields': 'message,from'}
        messages = api.get_object(f'/{conversation_id}/messages', **args)
        for message in messages['data']:
            if 'from' in message:
                msg_data = {
                    "User ID": message['from']['id'],
                    "User Name": message['from']['name'],
                    "Message": message['message']
                }
                all_messages.append(msg_data)
            else:
                print("No 'from' field found in the message.")
                print('-' * 50)
    return jsonify(all_messages)


def get_user_name(sender_id, page_access_token):
    url = f"https://graph.facebook.com/{sender_id}?fields=first_name,last_name&access_token={page_access_token}"
    response = requests.get(url)
    if response.status_code == 200:
        user_data = response.json()
        return f"{user_data['first_name']} {user_data['last_name']}"
    return "Unknown User"


@bp.route('/webhook', methods=['GET', 'POST'])
def webhook():
    if request.method == 'GET':
        token_sent = request.args.get("hub.verify_token")
        if token_sent == VERIFY_TOKEN:
            return request.args.get("hub.challenge", )
        return 'Invalid verification token'

    elif request.method == 'POST':
        data = request.get_json()
        if data['object'] == 'page':
            for entry in data['entry']:
                for messaging_event in entry['messaging']:
                    if 'message' in messaging_event:
                        sender_id = messaging_event['sender']['id']
                        user_name = get_user_name(sender_id, PAGE_ACCESS_TOKEN)
                        message_text = messaging_event['message']['text']
                        print(f"ID: {sender_id}")
                        print(f"Mensaje: {message_text}")
                        print(f"Nombre: {user_name}")
                        print('-' * 50)
                        # Respond to the user's message
                        response_text = modellingOpenAI(message_text)
                        send_message(sender_id, response_text)
        return jsonify({})

import requests

def send_message(recipient_id, text):
    """Send a response to Facebook"""
    payload = {
        'message': {
            'text': text
        },
        'recipient': {
            'id': recipient_id
        },
        'notification_type': 'regular'
    }
    
    auth = {
        'access_token': PAGE_ACCESS_TOKEN
    }

    response = requests.post(
        'https://graph.facebook.com/v17.0/me/messages',
        params=auth,
        json=payload
    )    
    return response.json()
