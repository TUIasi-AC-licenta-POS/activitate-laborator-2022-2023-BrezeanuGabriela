import React from 'react';
import Home from "./Home";
import Button from 'react-bootstrap/Button';

class Viewsongs extends React.Component {
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
            "http://localhost:8080/api/songcollection/songs/",
            {
                method: 'get',
                mode: 'cors',
                headers: {
                    'Access-Control-Allow-Origin': '*'
                }
            })
            .then((res) => {
                return res.json()
            })
            .then((json) => {
                // console.log(json);
                let _embedded = json._embedded;
                let musics = _embedded.musics;
                // for(let music of musics) {
                //     console.log(music.name);
                // }
                let _links = json._links;
                console.log(_embedded.musics);

                console.log(_links);
                this.setState({
                    items: musics,
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
    
                    <h1 > Music from Rest </h1>
                    <table >
                        <tr >
                            <th> Name </th>
                            <th> Genre</th>
                            <th> Type</th>
                            <th> Year</th>
                        </tr >
                        {
    
                            this.state.items.map((item) => (
    
    
                                <tr>
                                    <td > {item.name} </td>
                                    <td > {item.genre} </td>
                                    <td > {item.type} </td>
                                    <td > {item.year} </td>
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

export default Viewsongs;