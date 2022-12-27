import React from 'react';
import Home from "./Home";
import Button from 'react-bootstrap/Button';

class Viewartists extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            items: [],
            isLoaded: false,
            buttonPressed: ""
        };
        console.log(this.props);
    }

    setViewMusicDestination(message)
    {   
        this.setState({buttonPressed: message});
    }

    componentDidMount() {
        fetch(
            "http://localhost:8080/api/songcollection/artists/",
            {
                method: 'get',
                mode: 'cors',
                headers: {
                    'Access-Control-Allow-Origin': '*'
                }
            })
            .then((res) => {
                console.log(res);
                return res.json()
            })
            .then((json) => {
                // console.log(json);
                let _embedded = json._embedded;
                let artists = _embedded.artists;
                let _links = json._links;

                console.log(_links);
                this.setState({
                    items: artists,
                    isLoaded: true
                });
            })
            .catch((error) => {
                alert("Daca vrei sa mearga, porneste si Rest Sportify(SQL)!");
            });
    }

    render() {
        if(this.state.buttonPressed === "home")
        {
            return (
                <Home token={this.props.token}
                    username={this.props.username}
                    password={this.props.password} 
                    id={this.props.id}/>
            );
        }
        if (!this.state.isLoaded) {
            return (
                <>
                <p> Asteptati sa se incarce piesele. </p>
                <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
                </>
            )
        }
        else
        {
            return (
                <div className="App" >
    
                    <h1 > Artists from Rest </h1>
                    <table >
                        <tr >
                            <th> Name </th>
                        </tr >
                        {
    
                            this.state.items.map((item) => (
    
    
                                <tr>
                                    <td > {item.name} </td>
                                </tr>
                            ))
                        }
                    </table>
                    <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
                </div >
            );
        }
    };
}

export default Viewartists;