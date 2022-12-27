from models.role_orm import Role
from base.sql_base import Session


def get_roles():
    session = Session()
    roles = session.query(Role).all()
    role_names = list(map(lambda role: role.role_name, roles))
    result = ", ".join(role_names)
    return result

def create_role(role_name):
    session = Session()
    role = Role(role_name)
    try:
        session.add(role)
        session.commit()
    except Exception as exc:
        print(f"Failed to add role - {exc}")
    return role
