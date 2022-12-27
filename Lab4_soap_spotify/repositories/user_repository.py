import base64
import json
from datetime import datetime, timedelta
from pytime import pytime
import jwt
import uuid

from base.sql_base import Session
from models.role_orm import Role
from models.user_orm import User
from models.users_roles_orm import Users_Roles

HEADER = {
            "alg": "HS256",
            "typ": "JWT"
}
ISS = "http://127.0.0.1:8000"
KEY = "secret"
BLACKLIST_PATH = "/home/gabriela/an4/POS/proiect-v2/POS/Lab4_soap_spotify/blacklist.txt"

def read_blacklist():
    with open(BLACKLIST_PATH, "r") as blacklist_file_input:
        BLACKLIST = json.load(blacklist_file_input)

    return BLACKLIST

def write_blacklist(BLACKLIST):
    invalid_tokens = json.dumps(BLACKLIST, indent=4)
    with open(BLACKLIST_PATH, "w") as blacklist_file_output:
        blacklist_file_output.write(invalid_tokens)

def decode_payload(token):
    header, payload, signature = token.split(".")
    payload = base64.urlsafe_b64decode(payload)
    payload = payload.decode('utf-8')
    payload = json.loads(payload)

    return payload

def logout(token):
    BLACKLIST = read_blacklist()

    try:
        payload = jwt.decode(token, KEY, "HS256")
        uuid = payload['jti']
        BLACKLIST[uuid] = token
        write_blacklist(BLACKLIST)

    except Exception as e:
        # token-ul a fost corupt -> se decodeaza manual payload-ul pentru a extrage 'jti'
        payload = decode_payload(token)
        uuid = payload['jti']
        BLACKLIST[uuid] = token
        write_blacklist(BLACKLIST)

    return "Success"

def check_integrity_token(token):
    BLACKLIST = read_blacklist()

    try:
        payload = jwt.decode(token, KEY, "HS256")
        return payload

    # testul de integritate a picat
    except Exception as e:
        # se extrage manual payload-ul, iar din el 'jti'
        payload = decode_payload(token)
        uuid = payload['jti']
        BLACKLIST[uuid] = token
        write_blacklist(BLACKLIST)

        return None

def check_issuer(token, payload):
    BLACKLIST = read_blacklist()
    iss = payload['iss']

    return iss == ISS

def check_exp_date(token, payload):
    BLACKLIST = read_blacklist()

    local_time = datetime.now()
    # -2h pt ca el considera timpul ala pe alt fus orar...
    exp_date_from_token = datetime.fromtimestamp(payload['exp']) - timedelta(hours=2)
    print(local_time)
    print(exp_date_from_token)

    if exp_date_from_token <= local_time:
        # token expirat - add to blacklist
        uuid = payload['jti']
        BLACKLIST[uuid] = token
        write_blacklist(BLACKLIST)

        return False

    return True

def login_user(username, password):
    session = Session()
    user = session.query(User).filter(User.username == username).first()
    if user:
        if user.password == password:
            time = datetime.now()
            time = time + timedelta(hours=1)
            # time = time + timedelta(seconds=30)
            exp_date = time
            print(exp_date)

            payload = {
            "iss": ISS,
            "sub" : user.id,
            "exp" : exp_date,
            "jti" : str(uuid.uuid4())
            }

            encoded = jwt.encode(payload, KEY, algorithm="HS256")
            return encoded + "#" + str(user.id)
    return "Error:Username or password incorrect"

#black listez uuid ul, nu token ul
def authorize(token):
    session = Session()
    BLACKLIST = read_blacklist()

    # daca este deja blacklistat, nu mai are rost sa fie analizat
    if token not in BLACKLIST.values():
        payload = check_integrity_token(token)

        if payload:
            # testul pentru exp_date
            check_exp = check_exp_date(token, payload)
            if check_exp == False:
                return "Error:Expired token"

            check_iss = check_issuer(token, payload)
            if check_iss == False:
                return "Error:Issuer invalid"

            user = session.query(User).filter(User.id == payload['sub']).first()

            if user:
                roles_from_db = get_roles_for_user(user.username)
                roles = list(map(lambda role: role.role_name, roles_from_db))

                time = datetime.now() #miliseconds
                print(time)
                time = time + timedelta(hours=1)
                exp_date = time
                print(exp_date)

                payload = {
                    "iss": ISS,
                    "sub": user.id,
                    "exp": exp_date,
                    "jti": str(uuid.uuid4()),
                    "roles": roles
                }

                encoded = jwt.encode(payload, KEY, algorithm="HS256")
                return encoded
        else:
            return "Error:token invalid"

    return "Error:token black-listat"

def change_password(token, username, old_password, new_password):
    session = Session()
    BLACKLIST = read_blacklist()

    if token not in BLACKLIST.values():
        payload = check_integrity_token(token)
        if payload:
            check_exp = check_exp_date(token, payload)
            if check_exp == False:
                return "Error:Expired token"

            check_iss = check_issuer(token, payload)
            if check_iss == False:
                return "Error:Issuer invalid"

            user = session.query(User).filter(User.username == username).first()

            # verificare suplimentara pentru existenta user-ului
            if user:
                # verificare pentru id
                if user.id == payload['sub']:
                    if old_password != new_password:
                        if user.password == old_password:
                            user.password = new_password
                            session.commit()
                            return "Success"
                        else:
                            return "Error:Incorrect old password"
                    else:
                        return "Error:Password is the same"
                else:
                    # id-ul nu coincide, deci nu e cine se da de fapt => fara drept pentru a schimba parola
                   return "Error:fara drept"
            else:
                return "Error:invalid username"

        return "Error:invalid token"
    else:
        return "Error:token black-listat"

def create_user(token, username, password, roles):
    session = Session()
    BLACKLIST = read_blacklist()

    if token not in BLACKLIST.values():
        payload = check_integrity_token(token)
        if payload:
            check_exp = check_exp_date(token, payload)
            if check_exp == False:
                return "Error:Expired token"

            check_iss = check_issuer(token, payload)
            if check_iss == False:
                return "Error:Issuer invalid"

            # test pentru a verifica ca admin-ul face cererea
            id_admin = payload['sub']
            user_admin = get_user_by_id(id_admin)

            if user_admin:
                # verificam ca e admin pt a avea drept de create user
                if "administrator" in user_admin['roles']:
                    if get_user(username) != None:
                        return "Error: username already exists"

                    # ar mai trebui test si pt parola sa fie ok
                    user = User(username, password)

                    try:
                        session.add(user)
                    except Exception as exc:
                        print(f"Failed to add user - {exc}")
                        return "Error:cannot create user"

                    # adauga rolurile user ului
                    for role in roles.split('-'):
                        role_from_db = session.query(Role).filter(Role.role_name == role).first()
                        if role_from_db:
                            user_role = Users_Roles(user.id, role_from_db.id)
                            try:
                                session.add(user_role)
                            except Exception as exc:
                                print(f"Failed to add user_role - {exc}")
                                return "Error:cannot add role"
                        else:
                            return f"Error:The role ${role} doesn't exist"

                    session.commit()

                    return user.id
                else:
                    return "Error:fara drept"
            else:
                return "Error:id does not exist"
        else:
            return "Error:invalid token"
    else:
        return "Error:token black-listat"

def delete_user(token, username):
    session = Session()
    BLACKLIST = read_blacklist()

    if token not in BLACKLIST.values():
        payload = check_integrity_token(token)
        if payload:
            check_exp = check_exp_date(token, payload)
            if check_exp == False:
                return "Error:Expired token"

            check_iss = check_issuer(token, payload)
            if check_iss == False:
                return "Error:Issuer invalid"

            # test pentru a verifica ca admin-ul face cererea
            id_admin = payload['sub']
            user_admin = get_user_by_id(id_admin)

            if user_admin:
                # verificam ca e admin pt a avea drept de delete user
                if "administrator" in user_admin['roles']:
                    user = session.query(User).filter(User.username == username).first()
                    if user:
                        # delete roles
                        user_roles = session.query(Users_Roles).filter(Users_Roles.user_id == user.id).delete()
                        session.delete(user)
                        session.commit()
                        return "Success"
                    else:
                        return "Error:Username not found"
                else:
                    return "Error:fara drept"
            else:
                return "Error:id does not exist"
        else:
            return "Error:invalid token"
    else:
        return "Error:token black-listat"

def get_users(token):
    session = Session()

    BLACKLIST = read_blacklist()

    if token not in BLACKLIST.values():
        payload = check_integrity_token(token)
        if payload:
            check_exp = check_exp_date(token, payload)
            if check_exp == False:
                return "Error:Expired token"

            check_iss = check_issuer(token, payload)
            if check_iss == False:
                return "Error:Issuer invalid"

            # test pentru a verifica ca admin-ul face cererea
            id_admin = payload['sub']
            user_admin = get_user_by_id(id_admin)

            if user_admin:
                # verificam ca e admin pt a avea drept de delete user
                if "administrator" in user_admin['roles']:
                    #imi preiau toti userii care au roluri
                    users = session.query(
                        User
                    ).filter(
                        User.id == Users_Roles.user_id,
                    ).distinct(
                        User.username
                    ).order_by(
                        User.username
                    ).all()

                    result = []
                    for user in users:
                        password = user.password
                        username = user.username

                        user_roles = get_roles_for_user(username)

                        roles = []
                        for role in user_roles:
                            roles.append(role.role_name)

                        roles = ",".join(roles)

                        result.append({"username": username, "password": password, "roles": roles})

                    return result
                else:
                    return "Error:fara drept"
            else:
                return "Error:id does not exist"
        else:
            return "Error:invalid token"
    else:
        return "Error:token black-listat"

def get_user_with_token(token, username):
    session = Session()
    BLACKLIST = read_blacklist()

    if token not in BLACKLIST.values():
        payload = check_integrity_token(token)
        if payload:
            check_exp = check_exp_date(token, payload)
            if check_exp == False:
                return "Error:Expired token"

            check_iss = check_issuer(token, payload)
            if check_iss == False:
                return "Error:Issuer invalid"

            check_iss = check_issuer(token, payload)
            if check_iss == False:
                return "Error:Issuer invalid"

            # test pentru a verifica ca admin-ul face cererea
            id_admin = payload['sub']
            user_admin = get_user_by_id(id_admin)

            if user_admin:
                # verificam ca e admin pt a avea drept de delete user
                if "administrator" in user_admin['roles']:
                    user = get_user(username)
                    if user:
                        return user
                    else:
                        return "Error:username not found"
                else:
                    return "Error:fara drept"
            else:
                return "Error:id does not exist"
        else:
            return "Error:invalid token"
    else:
        return "Error:token black-listat"


# functii folosite intern
def get_user(username):
    session = Session()
    user = session.query(User).filter(User.username == username).first()

    if user:
        queries = session.query(
            User, Role, Users_Roles,
        ).filter(
            User.id == Users_Roles.user_id,
        ).filter(
            Users_Roles.role_id == Role.id
        ).filter(
            User.username == username
        ).all()

        roles = ",".join(list(map(lambda query: query[1].role_name, queries)))
        password = queries[0][0].password

        return {"username": username, "password": password, "roles": roles}
    else:
        return None

def get_user_by_id(id):
    session = Session()

    user = session.query(User).filter(User.id == id).first()

    queries = session.query(
        User, Role, Users_Roles,
    ).filter(
        User.id == Users_Roles.user_id,
    ).filter(
        Users_Roles.role_id == Role.id
    ).filter(
        User.id == id
    ).all()

    if queries:
        roles = ",".join(list(map(lambda query: query[1].role_name, queries)))
        password = queries[0][0].password
        username = queries[0][0].username

        return {"username": username, "password": password, "roles": roles}
    else:
        return None

def get_roles_for_user(username):
    session = Session()

    query = session.query(
        Role
    ).filter(
        User.id == Users_Roles.user_id,
    ).filter(
        Users_Roles.role_id == Role.id
    ).filter(
        User.username == username
    ).all()

    return query
