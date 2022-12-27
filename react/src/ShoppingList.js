class ShoppingList extends React.Component {
    // render() {
    //   return (
    //     <div className="shopping-list">
    //       <h1>Shopping List for {this.props.name}</h1>
    //       <ul>
    //         <li>Instagram</li>
    //         <li>WhatsApp</li>
    //         <li>Oculus</li>
    //       </ul>
    //     </div>
    //   );
    // }
    render() {
        return (
            <form id="login_form">
                <input type="text" placeholder="username"></input>
                <input type="password" placeholder="password"></input>
            </form>
        );
    }
}

  // Example usage: <ShoppingList name="Mark" />