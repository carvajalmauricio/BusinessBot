from flask import Blueprint, render_template, request, session
from flask_socketio import emit, SocketIO, join_room
import openai
import os
import firebase_admin
from firebase_admin import  db, credentials

bp = Blueprint('chatbot',__name__, url_prefix="/")
openai.api_key = 'xxxxxxxxxxxxxxxxxxxxxxxxxxx'
conversations = []
socketio = SocketIO()

credential_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'static', 'credentials.json')
cred = credentials.Certificate(credential_path)

firebase_admin.initialize_app(cred, {'databaseURL': 'https://businessbot-7f51b-default-rtdb.firebaseio.com/'})
ref = db.reference('/Users')

credential_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'static', 'credentials.json')
cred = credentials.Certificate(credential_path)

@bp.route('/<userId>', methods=['GET'])
def index(userId):
    try:
        User = ref.child(userId).get()
    except Exception as e:
        print("Error al obtener el usuario: ", e)   
    if request.method == 'GET':
        if User is None:
            return render_template('not-authorized.html')
        else:
            userName = User['userName'].split()
            firsName = userName[0]
            businessName = User['businessName']
            session['businessName'] = businessName
            session['BusinessInformation'] = User['Business Information']
            product_list = User['Products']
            # Usa compresión de listas para construir la cadena requerida:
            formatted_string = "-".join(f" nombre: {product['nombre']}, descripcion: {product['descripcion']}, cantidad: {product['cantidad']}, precio: {product['precio']} " for product in product_list)
            session['Products'] = formatted_string

            return render_template('index.html', firsName= firsName, businessName = businessName)
    else:
        return render_template('not-authorized.html')

@socketio.on('message')
def handle_message(data):
    question = data['data']
    businessName = session['businessName']
    session['BusinessInformation']['tonoDerespuesta']
    ciudad = session['BusinessInformation']['ciudad']
    contacto = session['BusinessInformation']['contacto']
    direccion = session['BusinessInformation']['direccion']
    horario = session['BusinessInformation']['horario']
    horarioApertura = session['BusinessInformation']['horarioApertura']
    horarioCierre = session['BusinessInformation']['horarioCierre']
    informacion = session['BusinessInformation']['informacion']
    products =session['Products']
    response = openai.ChatCompletion.create(
        model='gpt-3.5-turbo',
        messages=[
            {
                "role": "system",
                "content": f"""Este es un mensaje se system: De ahora en adelante eres un agente llamado Bot de ventas, del establecimiento {businessName} que se comunica con el Human por medio de redes sociales, tu tarea es comunicarte con el cliente con el objetivo de concretar una venta e intenta ser concreto y preciso
    , incluye emojis en tus respuestas de vez en cuenado,la informacion del negocio y sus productos disponibles estaran seguidos de '[]' [ciudad: {ciudad},telefono: {contacto},
    direccion: {direccion},horario:{horario}, horariodeapertura: {horarioApertura}, horariodecierre: {horarioCierre},
    informacion del establecimiento:{informacion}, productos disponibles: {products}] Caulquier informacion que el cliente, pregunte y no la encuentres en este texto, debes decir. "No te entendí, me lo repites?"
    Si el Human dice alguna palabra aluciendo a una despedida, deberás finalizar el chat. IMPORTANTE = Tus respuestas no deben pasar las 20 palabras
        Current conversation:
        {{history}}
        Human:
        {{input}}
        AI:"""
            },
            {
                "role": "user",
                "content": question
            }
        ],
        temperature=0.377,
        max_tokens=120,
        top_p=0.1,
        frequency_penalty=0.24,
        presence_penalty=0.1,
        stop="\n"
    )
    answer = response.choices[0]['message']['content'].strip()
    emit('response', {'data': answer}, room=request.sid)

@socketio.on('join')
def on_join(data):
    join_room(data['room'])